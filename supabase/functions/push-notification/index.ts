import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

import * as djwt from "https://deno.land/x/djwt@v2.8/mod.ts"

serve(async (req) => {
  try {
    const { record } = await req.json()
    const postId = record.post_id
    const commentContent = record.content
    const authorId = record.user_id

    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    // 1. 게시글 주인 찾기
    const { data: postData } = await supabase
      .from('community')
      .select('user_id')
      .eq('id', postId)
      .single()

    if (!postData) return new Response("Post not found", { status: 200 })
    const ownerId = postData.user_id

    // 2. 본인 제외 로직
    if (authorId === ownerId) {
        console.log("Self-comment detected, but proceeding for testing...");
      return new Response("Self-comment, skip", { status: 200 })
    }

    // 3. 주인 토큰 가져오기
    const { data: userData } = await supabase
      .from('users')
      .select('fcm_token')
      .eq('id', ownerId)
      .single()


    console.log(`[DEBUG] Retrieved FCM Token: ${userData?.fcm_token || 'TOKEN_NOT_FOUND'}`);

    if (!userData?.fcm_token) {
          console.log("Recipient has no FCM token");
          return new Response("No token", { status: 200 });
        }

    // 4. Firebase 인증 및 전송 (라이브러리 최소화 버전)
    // 이 부분은 서비스 계정 정보를 직접 활용합니다.
    const serviceAccount = JSON.parse(Deno.env.get('FIREBASE_SERVICE_ACCOUNT')!)

    // FCM v1은 OAuth2 토큰이 필요합니다.
    // 만약 계속 500 에러가 난다면, 일단 '발송 시도' 로그라도 찍히게 구성했습니다.
    console.log(`Sending notification to: ${ownerId}`)

    const now = Math.floor(Date.now() / 1000)
    const payload = {
      iss: serviceAccount.client_email,
      sub: serviceAccount.client_email,
      aud: "https://oauth2.googleapis.com/token",
      iat: now,
      exp: now + 3600,
      scope: "https://www.googleapis.com/auth/cloud-platform",
    }
    const pemContents = serviceAccount.private_key.replace(/\\n/g, '\n')
        const privateKeyData = await crypto.subtle.importKey(
          "pkcs8",
          Uint8Array.from(atob(pemContents.replace(/-----BEGIN PRIVATE KEY-----|-----END PRIVATE KEY-----|\n/g, "")), c => c.charCodeAt(0)),
          { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
          false,
          ["sign"]
        )

    const jwt = await djwt.create({ alg: "RS256", typ: "JWT" }, payload, privateKeyData)

    const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({
            grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
            assertion: jwt,
          }),
        })
        const { access_token } = await tokenResponse.json()

   // Google OAuth2 Access Token 생성
//        const jwtClient = new JWT(
//          serviceAccount.client_email,
//          undefined,
//          serviceAccount.private_key,
//          ['https://www.googleapis.com/auth/cloud-platform']
//        );

        const fcmResponse = await fetch(
              `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`,
              {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                  Authorization: `Bearer ${access_token}`, // 위에서 만든 토큰 사용
                },
                body: JSON.stringify({
                  message: {
                    token: userData.fcm_token,
                    notification: {
                      title: "새 댓글이 달렸습니다!",
                      body: commentContent,
                    },
                    data: {
                      postId: String(postId),
                    },
                    android: {
                      priority: "high", // 안드로이드 기기를 즉시 깨우도록 설정
                    },
                    apns: {
                      payload: {
                        aps: {
                          contentAvailable: true,
                        },
                      },
                    },
                  },
                }),
              }
            );

       const fcmResult = await fcmResponse.json();
           console.log(`FCM Response:`, JSON.stringify(fcmResult));

           return new Response(JSON.stringify({ success: true, fcmResult }), {
             status: 200,
             headers: { "Content-Type": "application/json" }
           })

  } catch (e) {
    console.error("CRITICAL ERROR:", e.message)
    return new Response(e.message, { status: 500 })
  }
})