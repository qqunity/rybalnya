package controllers

import (
	"encoding/json"
	"fmt"
	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/utils"
	"log"
	"net/http"
	"strconv"
)

func GetCommentsByPostID(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetCommentsResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetCommentsResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		postID, _ := strconv.ParseInt(r.FormValue("post_id"), 10, 64)
		comments, exitCode := db_interaction.GetCommentsByPostID(db_interaction.DBConnetor(true), uint64(postID), true)
		if exitCode == 0 || exitCode == 1 {
			response := http_response.GetCommentsResponseOk{
				Status:        true,
				Action:        "Данные о комметариях успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Comments: comments,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetCommentsResponseErr{
				Status: false,
				Error:  "Поста не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
