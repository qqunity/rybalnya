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

func AddUserInfo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	var user models.User
	_ = json.NewDecoder(r.Body).Decode(&user)
	exitCode := db_interaction.AddUserInfo(db_interaction.DBConnetor(true), &user, true)
	if exitCode != 0 {
		token, err := utils.CreateToken(int64(user.ID), user.Email)
		if err == nil {
			response := http_response.UserAddInfoResponseOk{
				Status:      true,
				Action:      "Пользователь успешно зарегистрирован!",
				UserID:      user.ID,
				AccessToken: token,
			}
			w.WriteHeader(http.StatusCreated)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			panic(fmt.Errorf("FATIL! Fatal error in generate token: %s", err))
		}
	} else {
		response := http_response.UserAddInfoResponseErr{
			Status: false,
			Error:  "Такой пользователь уже существует!",
		}
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode(response)
	}

}
