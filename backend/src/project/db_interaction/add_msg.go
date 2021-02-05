package db_interaction

import (
	"fmt"
	"github.com/go-pg/pg/v10"
	"github.com/my/repo/backend/src/project/models"
)

func AddMsg(db *pg.DB, msg *models.Message, shouldCloseDB bool) {
	_, err := db.Model(msg).
		Insert()
	if err != nil {
		panic(fmt.Errorf("FAIL. Fatal error with add Message in db: %s", err))
	}
	if shouldCloseDB {
		defer db.Close()
	}
}
func GetChatByRoomName(db *pg.DB, roomname string, shouldCloseDB bool) (Chat *models.Chat, exitCode uint) {
	exitCode = 0
	err := db.Model(Chat).
		Where("room_name = ?", roomname).
		Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get Chat info: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return Chat, exitCode
}
