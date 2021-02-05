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

func UpdateUserInfo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status:   false,
			Error:    "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status:   false,
			Error:    "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		var newUserInfo models.User
		_ = json.NewDecoder(r.Body).Decode(&newUserInfo)
		exitCode := db_interaction.UpdateUserInfo(db_interaction.DBConnetor(true), &newUserInfo, false)
		userInfo, _ := db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), newUserInfo.Email, true)
		if exitCode == 0 {
			response := http_response.UserUpdateInfoResponseOk{
				Status:        true,
				Action:        "Информация о пользователе успешно обновлена!",
				IsTokenExpire: utils.IsTokenExpire(r),
				UserInfo:      userInfo,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UserUpdateInfoResponseErr{
				Status: false,
				Error:  "Пользователя не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
