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

func GetMarkersInfoByLatLong(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method,  r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetMarkersInfoResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetMarkersInfoResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		tokenMetadata, err := utils.ExtractTokenMetadata(r)
		if err != nil {
			response := http_response.GetMarkersInfoResponseErr{
				Status: false,
				Error:  fmt.Sprintf("Проблема с метаданными токена: %s!", err),
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			markerInfo, exitCode := db_interaction.GetMarkersInfo(db_interaction.DBConnetor(true), tokenMetadata.UserID, true)
			if exitCode == 0 {
				response := http_response.GetMarkersInfoResponseOk{
					Status:        true,
					Action:        "Данные о маркерах успешно отправлены!",
					IsTokenExpire: utils.IsTokenExpire(r),
					MapInfo:       markerInfo,
				}
				w.WriteHeader(http.StatusOK)
				_ = json.NewEncoder(w).Encode(response)
			} else {
				response := http_response.GetMarkersInfoResponseErr{
					Status: false,
					Error:  "У пользователя нет маркеров!",
				}
				w.WriteHeader(http.StatusBadRequest)
				_ = json.NewEncoder(w).Encode(response)
			}
		}
	}
}
