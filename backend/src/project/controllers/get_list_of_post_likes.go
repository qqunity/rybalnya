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

func GetListOfPostLikes(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetListOfPostLikesResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetListOfPostLikesResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		postID, _ := strconv.ParseInt(r.FormValue("post_id"), 10, 64)
		likes, exitCode := db_interaction.GetListOfPostLikesByPostID(db_interaction.DBConnetor(true), uint64(postID), true)
		var userIDs []uint64
		for _, like := range likes {
			userIDs = append(userIDs, like.UserID)
		}
		if exitCode == 0 || exitCode == 1 {
			response := http_response.GetListOfPostLikesResponseOk{
				Status:        true,
				Action:        "Данные о количестве лайков успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				UserIDs:       userIDs,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetListOfPostLikesResponseErr{
				Status: false,
				Error:  "Поста не существует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}
