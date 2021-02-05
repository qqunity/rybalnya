package controllers

import (
	"encoding/json"
	"fmt"
	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/models"
	"github.com/my/repo/backend/src/project/utils"
	"log"
	"net/http"
)

func AddComment(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.AddCommentResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.AddCommentResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		tokenMetadata, err := utils.ExtractTokenMetadata(r)
		if err != nil {
			response := http_response.AddCommentResponseErr{
				Status: false,
				Error:  fmt.Sprintf("Проблема с метаданными токена: %s!", err),
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			var comment models.Comment
			_ = json.NewDecoder(r.Body).Decode(&comment)
			exitCode := db_interaction.AddComment(db_interaction.DBConnetor(true), comment.PostID, uint64(tokenMetadata.UserID), comment.Content, true)
			if exitCode == 0 {
				response := http_response.AddLikeResponseOk{
					Status: true,
					Action: "Комментарий успешно добавлен!",
					IsTokenExpire: utils.IsTokenExpire(r),
				}
				w.WriteHeader(http.StatusCreated)
				_ = json.NewEncoder(w).Encode(response)
			}  else {
				response := http_response.AddLikeResponseErr{
					Status: false,
					Error:  "Поста не существует!",
				}
				w.WriteHeader(http.StatusBadRequest)
				_ = json.NewEncoder(w).Encode(response)
			}
		}
	}
}
