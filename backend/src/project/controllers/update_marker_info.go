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

func UpdateMarkerInfo(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.UpdateMarkerInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.UpdateMarkerInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		id := r.FormValue("id")
		var newMarkerInfo models.MapInfo
		_ = json.NewDecoder(r.Body).Decode(&newMarkerInfo)
		markerInfo, exitCode := db_interaction.UpdateMapInfo(db_interaction.DBConnetor(true), id, &newMarkerInfo, false)
		if exitCode == 0 {
			response := http_response.UpdateMarkerInfoResponseOk{
				Status:        true,
				Action:        "Информация о маркере успешно обновлена!",
				IsTokenExpire: utils.IsTokenExpire(r),
				MapInfo:       markerInfo,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.UpdateMarkerInfoResponseErr{
				Status: false,
				Error:  "Маркера не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
