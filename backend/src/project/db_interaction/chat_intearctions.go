package db_interaction

import (
	"fmt"
	"strings"

	"github.com/go-pg/pg/v10"

	// "github.com/go-pg/pg/v10/orm"
	"github.com/my/repo/backend/src/project/models"
)

func LastMsgByRoomname(db *pg.DB, roomname string, shouldCloseDB bool) (message *models.Message, exitCode uint) {
	var chat models.Chat
	message = new(models.Message)
	exitCode = 0
	_ = db.Model(&chat).Where("roomname = ?", roomname).Select()
	_ = db.Model(message).Where("chat_id = ?", chat.ID).Order("id DESC").Last()
	if shouldCloseDB {
		defer db.Close()
	}
	return message, exitCode
}

func GetAllMessagesByRoomname(db *pg.DB, roomname string, shouldCloseDB bool) (messages []*models.Message, exitCode uint) {
	var chat models.Chat
	// var messages []*models.Message
	_ = db.Model(&chat).Where("roomname = ?", roomname).Select()
	err := db.Model(&messages).Where("chat_id = ?", chat.ID).Order("id ASC").Select()
	// err := db.Model(&chat).Where("roomname = ?", roomname).Column("chat.*").Relation("Messages").Select()
	// messages = chat.Messages
	exitCode = 0
	if err != nil {
		panic(err)
		// if err.Error() == "pg: no rows in result set" {
		// 	exitCode = 1
		// } else {
		// 	panic(fmt.Errorf("FAIL. Fatal error with get msg info: %s", err))
		// }
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return messages, exitCode
}

func CreateChatIfNotCreated(db *pg.DB, chat *models.Chat, shouldCloseDB bool) (exitCode uint) {
	// user, _ := GetUserInfoByEmail(db, chat.Users[0], false)
	// fmt.Println("user = ", user.String())
	_, exitCode = GetChatByRoomname(db, chat.Roomname, false)
	if exitCode != 0 {
		_, err := db.Model(chat).Insert()
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with add Chat in db: %s", err))
		}
	}
	if shouldCloseDB {
		defer db.Close()
	}
	return exitCode
}

func GetChatByRoomname(db *pg.DB, roomname string, shouldCloseDB bool) (chat *models.Chat, exitCode uint) {
	chat = new(models.Chat)
	exitCode = 0
	err := db.Model(chat).
		Where("roomname = ?", roomname).
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
	return chat, exitCode
}

func GetUsersWithLastMsgs(db *pg.DB, email string, shouldCloseDB bool) (users []*models.User, lastMsgs []*models.Message, online []bool, exitCode uint) {

	var chats []models.Chat
	exitCode = 0
	err := db.Model(&chats).Where("roomname LIKE '%' || ? || '%'", email).Column("chat.*").Relation("Messages").Select()
	if err != nil {
		if err.Error() == "pg: no rows in result set" {
			exitCode = 1
		} else {
			panic(fmt.Errorf("FAIL. Fatal error with get Chat info: %s", err))
		}
	}
	// fmt.Println(chats)

	for _, chat := range chats {
		if len(chat.Messages) != 0 {
			lastMsg := new(models.Message)
			_ = db.Model(lastMsg).Where("chat_id = ?", chat.ID).Order("id DESC").Last()
			lastMsgs = append(lastMsgs, lastMsg)
			members := strings.Split(chat.Roomname, "|")
			if members[0] == members[1] {
				user, _ := GetUserInfoByEmail(db, members[0], false)
				users = append(users, user)
				isOnline, _ := IsUserOnline(db, user.ID, false)
				online = append(online, isOnline)
			}
			if members[0] == email {
				user, _ := GetUserInfoByEmail(db, members[1], false)
				users = append(users, user)
				isOnline, _ := IsUserOnline(db, user.ID, false)
				online = append(online, isOnline)
			} else {
				user, _ := GetUserInfoByEmail(db, members[0], false)
				users = append(users, user)
				isOnline, _ := IsUserOnline(db, user.ID, false)
				online = append(online, isOnline)
			}
		}

	}

	if shouldCloseDB {
		defer db.Close()
	}
	return users, lastMsgs, online, exitCode
}
