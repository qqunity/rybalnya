package main

import (
	"fmt"

	"log"
	"net/http"
	"time"

	socketio "github.com/googollee/go-socket.io"

	"github.com/my/repo/backend/src/project/db_interaction"
	"github.com/my/repo/backend/src/project/models"
)

func main() {
	server, err := socketio.NewServer(nil)
	if err != nil {
		log.Fatal(err)
	}

	server.OnConnect("/", func(s socketio.Conn) error {
		s.SetContext("")
		fmt.Println("connected:", s.ID())
		return nil
	})

	server.OnEvent("/", "msg", func(s socketio.Conn, msg string, roomName string, sender string) {
		fmt.Println("msg:", msg, "in", roomName)
		var newMsg models.Message
		db := db_interaction.DBConnetor(true)
		User, _ := db_interaction.GetUserInfoByEmail(db, sender, false)
		Chat, _ := db_interaction.GetChatByRoomname(db, roomName, true)
		newMsg.Message = msg
		newMsg.ChatID = Chat.ID
		newMsg.UserID = User.ID
		newMsg.IsSeen = false
		db_interaction.AddMsg(db_interaction.DBConnetor(true), &newMsg, true)
		server.BroadcastToRoom("/", roomName, "newMsg", msg, sender, time.Now())
	})

	server.OnEvent("/", "msgRecive", func(s socketio.Conn, roomName string, checkerEmail string) {

		lastMsg, _ := db_interaction.LastMsgByRoomname(db_interaction.DBConnetor(true), roomName, true)
		fmt.Println("LAST MSG = ", lastMsg.Message)
		db_interaction.UpdateMsgCheck(db_interaction.DBConnetor(true), lastMsg.ID, true, true)
		server.BroadcastToRoom("/", roomName, "newCheck", checkerEmail)

	})

	server.OnEvent("/", "join", func(s socketio.Conn, roomName string, user string) {
		s.Join(roomName)
		fmt.Println("room: ", roomName)
		var chat models.Chat
		chat.Roomname = roomName
		_ = db_interaction.CreateChatIfNotCreated(db_interaction.DBConnetor(true), &chat, true)
		fmt.Println("Join success!")
		AllMsgs, _ := db_interaction.GetAllMessagesByRoomname(db_interaction.DBConnetor(true), roomName, true)
		MyUser, _ := db_interaction.GetUserInfoByEmail(db_interaction.DBConnetor(true), user, true)
		for _, Msg := range AllMsgs {
			if MyUser.ID != Msg.UserID {
				// fmt.Println("SOME MSG")
				db_interaction.UpdateMsgCheck(db_interaction.DBConnetor(true), Msg.ID, true, true)
			} else {
				db_interaction.UpdateMsgCheck(db_interaction.DBConnetor(true), Msg.ID, Msg.IsSeen, true)
			}
		}
		server.BroadcastToRoom("/", roomName, "newCheck", user)
	})

	server.OnError("/", func(s socketio.Conn, e error) {
		fmt.Println("meet error:", e)
	})

	server.OnDisconnect("/", func(s socketio.Conn, reason string) {
		server.LeaveAllRooms("/", s)
		fmt.Println("closed", reason)
	})

	server.OnEvent("/", "bye", func(s socketio.Conn) {
		s.LeaveAll()
		s.Close()
	})

	go server.Serve()
	defer server.Close()
	http.Handle("/", server)
	log.Println("Serving at localhost:8080...")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
