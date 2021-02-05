package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
	"strconv"
)

func GetPostsByUserID(db *pg.DB, userID uint64, shouldCloseDB bool) (posts []*models.Post, exitCode uint) {
	exitCode = 0
	err := db.Model(&posts).
		Where("user_id = ?", userID).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set"{
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get posts info: %s", err))
		}
	}
	if posts == nil {
		exitCode = 1
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return posts, exitCode
}

//todo: сортировка по времени
func GetAllPosts(db *pg.DB,  shouldCloseDB bool) (posts []*models.Post, exitCode uint) {
	exitCode = 0
	err := db.Model(&posts).
		Order("created_at DESC").
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set"{
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get posts info: %s", err))
		}
	}
	if posts == nil {
		exitCode = 1
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return posts, exitCode
}

func GetPostsWithConditions(db *pg.DB, startTime string, rawLimit string,  shouldCloseDB bool) (posts []*models.Post, exitCode uint) {
	exitCode = 0
	limit, _ := strconv.ParseInt(rawLimit, 0, 64)
	err := db.Model(&posts).
		Where("created_at >= ?", startTime).
		Order("created_at DESC").
		Limit(int(limit)).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set"{
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get posts info: %s", err))
		}
	}
	if posts == nil {
		exitCode = 1
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return posts, exitCode
}
