package main

import (
	"log"
	"net/http"

	"github.com/gorilla/mux"
	"github.com/my/repo/backend/src/project/controllers"
)

func main() {
	r := mux.NewRouter()
	r.HandleFunc("/add_user_info", controllers.AddUserInfo).Methods("POST")
	r.HandleFunc("/update_user_info", controllers.UpdateUserInfo).Methods("PUT")
	r.HandleFunc("/get_user_info", controllers.GetUserInfoByEmail).Methods("GET").Queries("email", "{email}")
	r.HandleFunc("/get_user_info", controllers.GetUserInfoByNickname).Methods("GET").Queries("nickname", "{nickname}")
	r.HandleFunc("/get_user_info", controllers.GetUserInfoByUserId).Methods("GET").Queries("user_id", "{user_id}")
	r.HandleFunc("/delete_user_info", controllers.DeleteUserInfoByEmail).Methods("DELETE").Queries("email", "{email}")
	r.HandleFunc("/login", controllers.LoginUser).Methods("POST")
	r.HandleFunc("/get_all_users_info", controllers.GetAllUsersInfo).Methods("GET")
	r.HandleFunc("/get_all_users_info_with_online", controllers.GetAllUsersInfoWithOnline).Methods("GET")
	r.HandleFunc("/user_online_true", controllers.UpdateUserOnlineTrue).Methods("PUT").Queries("Email", "{Email}")
	r.HandleFunc("/user_online_false", controllers.UpdateUserOnlineFalse).Methods("PUT").Queries("Email", "{Email}")

	r.HandleFunc("/add_map_marker", controllers.AddMapMarker).Methods("POST")
	r.HandleFunc("/get_markers_info", controllers.GetMarkersInfoByLatLong).Methods("GET")
	r.HandleFunc("/update_marker_info", controllers.UpdateMarkerInfo).Methods("PUT").Queries("id", "{id}")
	r.HandleFunc("/delete_marker_info", controllers.DeleteMarkerInfo).Methods("DELETE").Queries("id", "{id}")

	r.HandleFunc("/get_new_access_token", controllers.GetNewAccessToken).Methods("GET")

	r.HandleFunc("/add_post", controllers.AddPost).Methods("POST")
	r.HandleFunc("/get_all_posts", controllers.GetAllPosts).Methods("GET")
	r.HandleFunc("/get_all_posts_with_token", controllers.GetAllPostsByUserIDFromToken).Methods("GET")
	r.HandleFunc("/get_posts", controllers.GetAllPostsByUserID).Methods("GET").Queries("user_id", "{user_id}")
	r.HandleFunc("/get_posts_with_conditions", controllers.GetPostsWithCondition).Methods("GET").Queries("start_time", "{start_time}", "limit", "{limit}")
	r.HandleFunc("/delete_post", controllers.DeletePost).Methods("DELETE").Queries("id", "{id}")
	r.HandleFunc("/update_post", controllers.UpdatePost).Methods("PUT").Queries("id", "{id}")

	r.HandleFunc("/get_msgs_by_roomname", controllers.GetMsgsByRoomname).Methods("GET").Queries("roomname", "{roomname}")
	r.HandleFunc("/get_users_with_last_msgs", controllers.GetUsersWithLastMsgs).Methods("GET").Queries("email", "{email}")

	r.HandleFunc("/add_like", controllers.AddLike).Methods("POST").Queries("post_id", "{post_id}")
	r.HandleFunc("/delete_like", controllers.DeleteLike).Methods("DELETE").Queries("post_id", "{post_id}")
	r.HandleFunc("/get_count_of_likes", controllers.GetCountOfLikes).Methods("GET").Queries("post_id", "{post_id}")
	r.HandleFunc("/is_liked", controllers.IsLiked).Methods("GET").Queries("post_id", "{post_id}")
	r.HandleFunc("/get_list_of_post_likes", controllers.GetListOfPostLikes).Methods("GET").Queries("post_id", "{post_id}")

	r.HandleFunc("/add_comment", controllers.AddComment).Methods("POST")
	r.HandleFunc("/get_comments", controllers.GetCommentsByPostID).Methods("GET").Queries("post_id", "{post_id}")
	r.HandleFunc("/get_count_of_comments", controllers.GetCountOfCommentsByPostID).Methods("GET").Queries("post_id", "{post_id}")
	r.HandleFunc("/update_comment", controllers.UpdateComment).Methods("PUT").Queries("id", "{id}")
	r.HandleFunc("/delete_comment", controllers.DeleteComment).Methods("DELETE").Queries("id", "{id}")

	log.Fatal(http.ListenAndServe(":8000", r))
}
