package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddPost(db *pg.DB, post *models.Post, shouldCloseDB bool) {
	_, err := db.Model(post).
		Insert()
	if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add post in db: %s", err))
		}
	if shouldCloseDB{
		defer db.Close()
	}
}
