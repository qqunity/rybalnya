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

func LoginUser(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	var loginInfo models.User
	_ = json.NewDecoder(r.Body).Decode(&loginInfo)
	user, _ := db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), loginInfo.Email, false)
	isLoginSuccess, exitCode := db_interaction.LoginUser(db_interaction.DBConnetor(true), loginInfo.Email, loginInfo.Password, true)
	if exitCode == 0 && isLoginSuccess {
		token, err := utils.CreateToken(int64(user.ID), user.Email)
		if err == nil {
			response := http_response.UserLoginResponseOk{
				Status:      true,
				Action:      "Успешная аутентификация!",
				AccessToken: token,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			panic(fmt.Errorf("FATIL! Fatal error in generate token: %s", err))
		}
	} else {
		response := http_response.UserLoginResponseErr{
			Status: false,
			Error:  "Неправильно введены данные!",
		}
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode(response)
	}
}
