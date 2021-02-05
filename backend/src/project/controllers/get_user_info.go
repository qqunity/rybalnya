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

func GetUserInfoByEmail(w http.ResponseWriter, r *http.Request) {
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
		userInfo, exitCode := db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), email, true)
		if exitCode == 0 {
			response := http_response.UserGetInfoResponseOk{
				Status:        true,
				Action:        "Данные о пользователе успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				UserInfo:      userInfo,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UserGetInfoResponseErr{
				Status:   false,
				Error:    "Такой почты не существует!",
				UserInfo: "null",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}

func GetUserInfoByNickname(w http.ResponseWriter, r *http.Request) {
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
		nickname := r.FormValue("nickname")
		usersInfo, exitCode := db_interaction.GetUsersInfoByNickname(db_interaction.DBConnetor(true), nickname, true)
		if exitCode == 0 {
			response := http_response.UsersGetInfoResponseOk{
				Status:        true,
				Action:        "Данные о пользователе успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				UsersInfo:     usersInfo,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UserGetInfoResponseErr{
				Status:   false,
				Error:    "Такого имени не сусществует!",
				UserInfo: "null",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}

func GetUserInfoByUserId(w http.ResponseWriter, r *http.Request) {
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
		userId, _ := strconv.ParseInt(r.FormValue("user_id"), 10, 64)
		userInfo, exitCode := db_interaction.GetUserInfoByUserId(db_interaction.DBConnetor(true), uint64(userId), true)
		if exitCode == 0 {
			response := http_response.UserGetInfoResponseOk{
				Status:        true,
				Action:        "Данные о пользователе успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				UserInfo:      userInfo,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UserGetInfoResponseErr{
				Status:   false,
				Error:    "Такого пользователя не существует!",
				UserInfo: "null",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
