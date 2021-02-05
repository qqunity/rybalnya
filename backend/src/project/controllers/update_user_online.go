package controllers

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"strconv"

	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/models"
	"github.com/my/repo/backend/src/project/utils"
)

func UpdateUserOnline(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		var user *models.User
		var exitCode uint = 0
		userEmail := r.FormValue("Email")
		isOnline, _ := strconv.ParseBool(r.FormValue("Online"))
		data, _ := utils.ExtractTokenMetadata(r)
		if data.UserEmail == userEmail {
			user, _ = db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), userEmail, false)
			exitCode = db_interaction.UpdateUserOnline(db_interaction.DBConnetor(true), user.ID, isOnline, true)
		} else {
			exitCode = 1
		}
		if exitCode == 0 {
			response := http_response.UpdateUserOnlineOk{
				Status: true,
				Action: "Информация о пользователе успешно обновлена!",
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UpdateUserOnlineErr{
				Status: false,
				Error:  "Пользователя не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
func UpdateUserOnlineTrue(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		var user *models.User
		var exitCode uint = 0
		userEmail := r.FormValue("Email")
		isOnline := true
		data, _ := utils.ExtractTokenMetadata(r)
		if data.UserEmail == userEmail {
			user, _ = db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), userEmail, false)
			exitCode = db_interaction.UpdateUserOnline(db_interaction.DBConnetor(true), user.ID, isOnline, true)

		} else {
			exitCode = 1
		}
		if exitCode == 0 {
			response := http_response.UpdateUserOnlineOk{
				Status: true,
				Action: "Информация о пользователе успешно обновлена!",
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UpdateUserOnlineErr{
				Status: false,
				Error:  "Пользователя не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
func UpdateUserOnlineFalse(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UserUpdateInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		var user *models.User
		var exitCode uint = 0
		userEmail := r.FormValue("Email")
		isOnline := false
		data, _ := utils.ExtractTokenMetadata(r)
		if data.UserEmail == userEmail {
			user, _ = db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), userEmail, false)
			exitCode = db_interaction.UpdateUserOnline(db_interaction.DBConnetor(true), user.ID, isOnline, true)
		} else {
			exitCode = 1
		}
		if exitCode == 0 {
			response := http_response.UpdateUserOnlineOk{
				Status: true,
				Action: "Информация о пользователе успешно обновлена!",
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UpdateUserOnlineErr{
				Status: false,
				Error:  "Пользователя не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
