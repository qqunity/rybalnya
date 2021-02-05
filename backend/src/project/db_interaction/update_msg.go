package db_interaction

import (
	"fmt"

	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func UpdateMsgCheck(db *pg.DB, id uint64, isSeen bool, shouldCloseDB bool) (updatedMsg *models.Message, exitCode uint) {
	exitCode = 0
	oldMsg := new(models.Message)
	err := db.Model(oldMsg).
		Where("id = ?", id).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with update comment info: %s", err))
		}
	}
	msg := new(models.Message)
	if exitCode == 0 {
		_, err := db.Model(msg).
			Set("user_id = ?", oldMsg.UserID).
			Set("chat_id = ?", oldMsg.ChatID).
			Set("message = ?", oldMsg.Message).
			Set("is_seen = ?", isSeen).
			Set("is_edited = ?", false).
			Set("created_at = ?", oldMsg.CreatedAt).
			Where("id = ?", id).
			Update()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with update msg seen info in db: %s", err))
		}
		err = db.Model(updatedMsg).
			Where("id = ?", id).
			Select()
	}

	if shouldCloseDB {
		defer db.Close()
	}
	return updatedMsg, exitCode
}
