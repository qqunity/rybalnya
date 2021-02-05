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

func DeleteMarkerInfo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s|", r.Method, r.Header))
	if utils.IsExpiredToken(r) {
		response := http_response.DeleteMarkerInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.DeleteMarkerInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		id := r.FormValue("id")
		exitCode := db_interaction.DeleteMarkerInfo(db_interaction.DBConnetor(true), id,
			true)
		if exitCode == 0 {
			response := http_response.DeleteMarkerInfoResponseOk{
				Status:        true,
				Action:        "Маркер пользователя удален!",
				IsTokenExpire: utils.IsTokenExpire(r),
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.DeleteMarkerInfoResponseErr{
				Status: false,
				Error:  "Маркер не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}

	}
}
