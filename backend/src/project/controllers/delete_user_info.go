package controllers

import (
	"encoding/json"
	"fmt"
	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/utils"
	"log"
	"net/http"
)

func DeleteUserInfoByEmail(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UserGetInfoResponseErr{
			Status:   false,
			Error:    "Токен устарел!",
			UserInfo: "null",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UserGetInfoResponseErr{
			Status:   false,
			Error:    "Токен некорректный!",
			UserInfo: "null",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		email := r.FormValue("email")
		exitCode := db_interaction.DeleteUserInfoByEmail(db_interaction.DBConnetor(true), email, true)
		if exitCode == 0 {
			response := http_response.UserDeleteInfoResponseOk{
				Status:        true,
				Action:        "Аккаунт пользователя удален!",
				IsTokenExpire: utils.IsTokenExpire(r),
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UserDeleteInfoResponseErr{
				Status: false,
				Error:  "Пользователь не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
