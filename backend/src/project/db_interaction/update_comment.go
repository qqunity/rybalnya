package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdateComment(db *pg.DB, id string, content string, shouldCloseDB bool) (updatedComment *models.Comment, exitCode uint) {
	exitCode = 0
	oldComment := new(models.Comment)
	err := db.Model(oldComment).
		Where("id = ?", id).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with update comment info: %s", err))
		}
	}
	comment := new(models.Comment)
	if exitCode == 0 {
		_, err := db.Model(comment).
			Set("user_id = ?", oldComment.UserID).
			Set("post_id = ?", oldComment.PostID).
			Set("content = ?", content).
			Set("was_updated = ?", true).
			Set("created_at = ?", oldComment.CreatedAt).
			Where("id = ?", id).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update comment info in db: %s", err))
		}
		err = db.Model(updatedComment).
			Where("id = ?", id).
			Select()
	}

	if shouldCloseDB {
		defer db.Close()
	}
	return updatedComment, exitCode
}
