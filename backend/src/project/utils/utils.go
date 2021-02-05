package utils

import (
	"github.com/dgrijalva/jwt-go"
	"github.com/my/repo/backend/src/project/models"
	"net/http"
	"time"
)

func CreateToken(userId int64, userEmail string) (string, error) {
	atClaims := jwt.MapClaims{}
	atClaims["user_id"] = userId
	atClaims["user_email"] = userEmail
	atClaims["exp"] = time.Now().Add(time.Hour * 730).Unix()
	at := jwt.NewWithClaims(jwt.SigningMethodHS256, atClaims)
	token, err := at.SignedString([]byte("secret"))
	if err != nil {
		return "", err
	}
	return token, nil
}

func ExtractToken(r *http.Request) string {
	return r.Header.Get("Access-Token")
}

func ExtractTokenMetadata(r *http.Request) (accessData models.AccessDetails, err error) {
	tokenString := ExtractToken(r)
	claims := jwt.MapClaims{}
	_, err = jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		return []byte("secret"), nil
	})
	if err == nil {
		accessData = models.AccessDetails{
			UserEmail: claims["user_email"].(string),
			UserID:    int64(claims["user_id"].(float64)),
			CreatedAt: time.Unix(int64(claims["exp"].(float64)), 0),
		}
	}
	return accessData, err
}

func IsExpiredToken(r *http.Request) bool {
	_, err := ExtractTokenMetadata(r)
	return err != nil && err.Error() == "Token is expired"
}

func IsTokenCorrect(r *http.Request) bool {
	_, err := ExtractTokenMetadata(r)
	return err == nil
}

func IsTokenExpire(r *http.Request) bool {
	accessDetails, _ := ExtractTokenMetadata(r)
	return time.Since(accessDetails.CreatedAt).Minutes() >= -20
}
