package main

import (
	"fmt"

	"log"
	"net/http"

	socketio "github.com/googollee/go-socket.io"
)

func main() {
	server, err := socketio.NewServer(nil)
	if err != nil {
		log.Fatal(err)
	}

	server.OnConnect("/", func(s socketio.Conn) error {
		s.SetContext("")
		s.Join("allOnlineUsers")
		fmt.Println("online:", s.ID())
		return nil
	})

	server.OnEvent("/", "userOnline", func(s socketio.Conn, user_email string) {
		server.BroadcastToRoom("/", "allOnlineUsers", "newOnline", user_email)
		fmt.Println("userOnline: " + user_email)

	})

	server.OnEvent("/", "userOffline", func(s socketio.Conn, user_email string) {
		server.BroadcastToRoom("/", "allOnlineUsers", "newOffline", user_email)
		fmt.Println("newOffline: " + user_email)

	})
	server.OnError("/", func(s socketio.Conn, e error) {
		fmt.Println("meet error:", e)
	})

	server.OnDisconnect("/", func(s socketio.Conn, reason string) {
		fmt.Println("closed", reason)
	})

	server.OnEvent("/", "bye", func(s socketio.Conn) {
		s.LeaveAll()
		s.Close()
	})

	go server.Serve()
	defer server.Close()
	http.Handle("/", server)
	log.Println("Serving at localhost:7777...")
	log.Fatal(http.ListenAndServe(":7777", nil))
}
