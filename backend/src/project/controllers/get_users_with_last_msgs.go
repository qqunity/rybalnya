package controllers

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/utils"
)

func GetUsersWithLastMsgs(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetAllUsersResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetAllUsersResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		email := r.FormValue("email")
		users, lastMsgs, onlines, exitCode := db_interaction.GetUsersWithLastMsgs(db_interaction.DBConnetor(true), email, true)
		for _, user := range users {
			user.Password = ""
		}
		metadataToken, _ := utils.ExtractTokenMetadata(r)
		if (exitCode == 0 || exitCode == 1) && metadataToken.UserEmail == email {
			response := http_response.GetUsersWithLastMsgsResponseOk{
				Status:        true,
				Action:        "Данные о пользователях успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Online:        onlines,
				Users:         users,
				LastMsgs:      lastMsgs,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetUsersWithLastMsgsResponseErr{
				Status: false,
				Error:  "Нет сообщений!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
