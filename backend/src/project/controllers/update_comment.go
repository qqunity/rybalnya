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

func UpdateComment(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UpdateCommentResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UpdateCommentResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		id := r.FormValue("id")
		var newComment models.Comment
		_ = json.NewDecoder(r.Body).Decode(&newComment)
		comment, exitCode := db_interaction.UpdateComment(db_interaction.DBConnetor(true), id, newComment.Content, false)
		if exitCode == 0 {
			response := http_response.UpdateCommentResponseOk{
				Status:        true,
				Action:        "Информация о комметарии успешно обновлена!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Comment:       comment,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UpdateCommentResponseErr{
				Status: false,
				Error:  "Комментария не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
