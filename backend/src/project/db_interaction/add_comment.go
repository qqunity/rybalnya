package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddComment(db *pg.DB, postID uint64, userID uint64, content string, shouldCloseDB bool) (exitCode uint) {
	comment := models.Comment{
		UserID: userID,
		PostID: postID,
		Content: content,
	}
	post := new(models.Post)
	exitCode = 0
	err := db.Model(post).
		Where("id = ?", postID).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set"{
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with add comment info: %s", err))
		}
	}
	if exitCode == 0 {
		_, err = db.Model(&comment).
			Insert()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add comment in db: %s", err))
		}
	}
	if shouldCloseDB{
		defer db.Close()
	}
	return exitCode
}
