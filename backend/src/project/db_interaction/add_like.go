package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddLike(db *pg.DB, postID uint64, userID uint64, shouldCloseDB bool) (exitCode uint) {
	like := models.Like{
		UserID: userID,
		PostID: postID,
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
			panic(fmt.Errorf("FAIL. Fatal error with add like info: %s", err))
		}
	}
	var someLike *models.Like
	err = db.Model(someLike).Where("(post_id, user_id) = (?, ?)", postID, userID).Select()
	if err.Error() != "pg: no rows in result set" {
		exitCode = 2
	}
	if exitCode == 0 {
		_, err = db.Model(&like).
			Insert()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add like in db: %s", err))
		}
	}
	if shouldCloseDB{
		defer db.Close()
	}
	return exitCode
}
