package controllers

import (
	"encoding/json"
	"fmt"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/utils"
	"log"
	"net/http"
)

func GetNewAccessToken(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetNewAccessTokenResponseErr{
			Status:   false,
			Error:    "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetNewAccessTokenResponseErr{
			Status:   false,
			Error:    "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		accessDetails, _ := utils.ExtractTokenMetadata(r)
		token, err := utils.CreateToken(accessDetails.UserID, accessDetails.UserEmail)
		if err == nil {
			response := http_response.UserLoginResponseOk{
				Status:          true,
				Action:          "Токен был обновлен успешно!",
				AccessToken:     token,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			panic(fmt.Errorf("FATIL! Fatal error in generate token: %s", err))
		}
	}
}
