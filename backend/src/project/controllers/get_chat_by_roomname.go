package controllers

import (
	"encoding/json"
	// "fmt"
	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/http_response"
	"github.com/my/repo/backend/src/project/utils"
	// "log"
	"net/http"
)

func GetMsgsByRoomname(w http.ResponseWriter, r *http.Request) {
	// w.Header().Set("Content-Type", "application/json")
	// log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	// if utils.IsExpiredToken(r) {
	// 	response := http_response.GetChatByRoomnameResponceErr{
	// 		Status: false,
	// 		Error:  "Токен устарел!",
	// 	}
	// 	w.WriteHeader(http.StatusUnauthorized)
	// 	_ = json.NewEncoder(w).Encode(response)
	// } else if !utils.IsTokenCorrect(r) {
	// 	response := http_response.GetChatByRoomnameResponceErr{
	// 		Status: false,
	// 		Error:  "Токен некорректный!",
	// 	}
	// 	w.WriteHeader(http.StatusUnauthorized)
	// 	_ = json.NewEncoder(w).Encode(response)
	// } else {
	roomname := r.FormValue("roomname")
	msgs, exitCode := db_interaction.GetAllMessagesByRoomname(db_interaction.DBConnetor(true), roomname, true)
	if exitCode == 0 || exitCode == 1 {
		response := http_response.GetChatByRoomnameResponceOk{
			Status:        true,
			Action:        "Данные о сообщениях отправлены!",
			IsTokenExpire: utils.IsTokenExpire(r),
			AllMsgs:       msgs,
		}
		w.WriteHeader(http.StatusOK)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		response := http_response.GetChatByRoomnameResponceErr{
			Status: false,
			Error:  "Чата не существует!",
		}
		w.WriteHeader(http.StatusBadRequest)
		_ = json.NewEncoder(w).Encode(response)
	}
	// }
}
