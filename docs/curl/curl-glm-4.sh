curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiMDQ2MTgzYjMyYjkwNDg0NDk0OWJkMDYyYjFhYjIyM2MiLCJleHAiOjE3NDI5NjAwNjYxMzgsInRpbWVzdGFtcCI6MTc0Mjk1ODI2NjE0OH0.i5r5O7IPlVLBxrbORsNsOgs0_My9C_wRBFwTXFB-dSM" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
