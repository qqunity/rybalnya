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

func GetAllUsersInfo(w http.ResponseWriter, r *http.Request) {
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
		users, exitCode := db_interaction.GetAllUsersInfo(db_interaction.DBConnetor(true), true)
		for _, user := range users {
			user.Password = ""
		}
		if exitCode == 0 || exitCode == 1 {
			response := http_response.GetAllUsersResponseOk{
				Status:        true,
				Action:        "Данные о пользователях успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Users:         users,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetAllUsersResponseErr{
				Status: false,
				Error:  "Поста не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
func GetAllUsersInfoWithOnline(w http.ResponseWriter, r *http.Request) {
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
		online, users, exitCode := db_interaction.GetAllUsersInfoWithOnline(db_interaction.DBConnetor(true), true)
		for _, user := range users {
			user.Password = ""
		}
		if exitCode == 0 || exitCode == 1 {
			response := http_response.GetAllUsersWithOmlineResponseOk{
				Status:        true,
				Action:        "Данные о пользователях успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Online:        online,
				Users:         users,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetAllUsersWithOmlineResponseErr{
				Status: false,
				Error:  "Ошибка с получением данных о пользователях",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
