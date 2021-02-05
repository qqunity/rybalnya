package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func IsLiked(db *pg.DB, postID uint64, userID uint64, shouldCloseDB bool) (isLiked bool, exitCode uint) {
	isLiked = false
	post := new(models.Post)
	exitCode = 0
	err := db.Model(post).
		Where("id = ?", postID).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with check like info: %s", err))
		}
	}
	if exitCode == 0 {
		like := new(models.Like)
		err := db.Model(like).
			Where("(post_id, user_id) = (?, ?)", postID, userID).
			Select()
		if err == nil {
			isLiked = true
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return isLiked, exitCode
}
