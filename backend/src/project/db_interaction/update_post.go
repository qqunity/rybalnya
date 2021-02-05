package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdatePost(db *pg.DB, id string, newPost *models.Post, shouldCloseDB bool) (somePost *models.Post, exitCode uint) {
	updatedPost := new(models.Post)
	exitCode = 0
	var post models.Post
	err := db.Model(&post).
		Where("id = ?", id).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with update post info: %s", err))
		}
	}
	if exitCode == 0 {
		_, err := db.Model(somePost).
			Set("description = ?", func() string {
				if newPost.Description != "" {
					return newPost.Description
				} else {
					return post.Description
				}
			}()).
			Set("image = ?", func() []byte {
				if newPost.Image != nil {
					return newPost.Image
				} else {
					return post.Image
				}
			}()).
			Set("user_id = ?", post.UserID).
			Set("created_at = ?", post.CreatedAt).
			Where("id = ?", post.ID).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update post info in db: %s", err))
		}
		err = db.Model(updatedPost).Where("id = ?", post.ID).Select()
	}

	if shouldCloseDB {
		defer db.Close()
	}
	return updatedPost, exitCode
}
