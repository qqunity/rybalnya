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

func GetAllPosts(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		posts, exitCode := db_interaction.GetAllPosts(db_interaction.DBConnetor(true), true)
		if exitCode == 0 {
			response := http_response.GetPostsResponseOk{
				Status:        true,
				Action:        "Данные о постах успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Posts:         posts,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetPostsResponseErr{
				Status: false,
				Error:  "Таких постов не сусществует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}

func GetAllPostsByUserIDFromToken(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		tokenMetadata, err := utils.ExtractTokenMetadata(r)
		if err != nil {
			response := http_response.AddMapMarkerResponseErr{
				Status: false,
				Error:  fmt.Sprintf("Проблема с метаданными токена: %s!", err),
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			posts, exitCode := db_interaction.GetPostsByUserID(db_interaction.DBConnetor(true), uint64(tokenMetadata.UserID), true)
			if exitCode == 0 {
				response := http_response.GetPostsResponseOk{
					Status:        true,
					Action:        "Данные о постах успешно отправлены!",
					IsTokenExpire: utils.IsTokenExpire(r),
					Posts:         posts,
				}
				w.WriteHeader(http.StatusOK)
				_ = json.NewEncoder(w).Encode(response)
			} else {
				response := http_response.GetPostsResponseErr{
					Status: false,
					Error:  "Таких постов не сусществует!",
				}
				w.WriteHeader(http.StatusBadRequest)
				_ = json.NewEncoder(w).Encode(response)
			}
		}
	}
}

func GetAllPostsByUserID(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		userID, _ := strconv.ParseInt(r.FormValue("user_id"), 10, 64)
		posts, exitCode := db_interaction.GetPostsByUserID(db_interaction.DBConnetor(true), uint64(userID), true)
		if exitCode == 0 {
			response := http_response.GetPostsResponseOk{
				Status:        true,
				Action:        "Данные о постах успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Posts:         posts,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetPostsResponseErr{
				Status: false,
				Error:  "Таких постов не сусществует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}
	}
}

func GetPostsWithCondition(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	log.Print(fmt.Sprintf("REQUEST: Method |%s| \t Headers: |%s| \t Route: |%s|", r.Method, r.Header, r.RequestURI))
	if utils.IsExpiredToken(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен устарел!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else if !utils.IsTokenCorrect(r) {
		response := http_response.GetPostsResponseErr{
			Status: false,
			Error:  "Токен некорректный!",
		}
		w.WriteHeader(http.StatusUnauthorized)
		_ = json.NewEncoder(w).Encode(response)
	} else {
		startTime := r.FormValue("start_time")
		limit := r.FormValue("limit")
		fmt.Println(limit)
		posts, exitCode := db_interaction.GetPostsWithConditions(db_interaction.DBConnetor(true), startTime, limit, true)
		if exitCode == 0 {
			response := http_response.GetPostsResponseOk{
				Status:        true,
				Action:        "Данные о постах успешно отправлены!",
				IsTokenExpire: utils.IsTokenExpire(r),
				Posts:         posts,
			}
			w.WriteHeader(http.StatusOK)
			_ = json.NewEncoder(w).Encode(response)
		} else {
			response := http_response.GetPostsResponseErr{
				Status: false,
				Error:  "Таких постов не сусществует!",
			}
			w.WriteHeader(http.StatusBadRequest)
			_ = json.NewEncoder(w).Encode(response)
		}

	}
}
