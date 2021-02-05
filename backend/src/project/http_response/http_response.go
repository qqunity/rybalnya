package http_response

import "github.com/my/repo/backend/src/project/models"

type UserAddInfoResponseOk struct {
	Status      bool
	Action      string
	UserID      uint64
	AccessToken string
}

type UserAddInfoResponseErr struct {
	Status bool
	Error  string
}

type UserGetInfoResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	UserInfo      *models.User
}

type UsersGetInfoResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	UsersInfo     []models.User
}

type UserGetInfoResponseErr struct {
	Status   bool
	Error    string
	UserInfo string
}

type UserLoginResponseOk struct {
	Status      bool
	Action      string
	AccessToken string
}

type UserLoginResponseErr struct {
	Status bool
	Error  string
}

type UserUpdateInfoResponseErr struct {
	Status bool
	Error  string
}

type UserUpdateInfoResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	UserInfo      *models.User
}

type UserDeleteInfoResponseErr struct {
	Status bool
	Error  string
}

type UserDeleteInfoResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type GetNewAccessTokenResponseOk struct {
	Status      bool
	Action      string
	AccessToken string
}

type GetNewAccessTokenResponseErr struct {
	Status bool
	Error  string
}

type AddMapMarkerResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
}

type AddMapMarkerResponseErr struct {
	Status bool
	Error  string
}

type GetMarkersInfoResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	MapInfo       []models.MapInfo
}

type GetMarkersInfoResponseErr struct {
	Status bool
	Error  string
}

type UpdateMarkerInfoResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	MapInfo       *models.MapInfo
}

type UpdateMarkerInfoResponseErr struct {
	Status bool
	Error  string
}

type DeleteMarkerInfoResponseErr struct {
	Status bool
	Error  string
}

type DeleteMarkerInfoResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type AddPostResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
}

type AddPostResponseErr struct {
	Status bool
	Error  string
}

type GetPostsResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Posts         []*models.Post
}

type GetPostsResponseErr struct {
	Status bool
	Error  string
}

type DeletePostResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type DeletePostResponseErr struct {
	Status bool
	Error  string
}

type UpdatePostResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Post          *models.Post
}

type UpdatePostResponseErr struct {
	Status bool
	Error  string
}

type GetAllUsersResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Users         []*models.User
}

type GetAllUsersResponseErr struct {
	Status bool
	Error  string
}

type GetChatByRoomnameResponceOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	AllMsgs       []*models.Message
}

type GetChatByRoomnameResponceErr struct {
	Status bool
	Error  string
}

type AddLikeResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type AddLikeResponseErr struct {
	Status bool
	Error  string
}

type DeleteLikeResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type DeleteLikeResponseErr struct {
	Status bool
	Error  string
}

type GetCountOfLikesResponseOk struct {
	Status        bool
	IsTokenExpire bool
	CntLikes      uint64
	Action        string
}

type GetCountOfLikesResponseErr struct {
	Status bool
	Error  string
}

type IsLikedResponseOk struct {
	Status        bool
	IsTokenExpire bool
	IsLiked       bool
	Action        string
}

type IsLikedResponseErr struct {
	Status bool
	Error  string
}

type GetListOfPostLikesResponseOk struct {
	Status        bool
	IsTokenExpire bool
	UserIDs       []uint64
	Action        string
}

type GetListOfPostLikesResponseErr struct {
	Status bool
	Error  string
}

type AddCommentResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type AddCommentResponseErr struct {
	Status bool
	Error  string
}

type GetCommentsResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Comments      []models.Comment
	Action        string
}

type GetCommentsResponseErr struct {
	Status bool
	Error  string
}

type GetCountOfCommentsResponseOk struct {
	Status        bool
	IsTokenExpire bool
	CntComments   uint64
	Action        string
}

type GetCountOfCommentsResponseErr struct {
	Status bool
	Error  string
}

type UpdateCommentResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Comment       *models.Comment
}

type UpdateCommentResponseErr struct {
	Status bool
	Error  string
}

type DeleteCommentResponseOk struct {
	Status        bool
	IsTokenExpire bool
	Action        string
}

type DeleteCommentResponseErr struct {
	Status bool
	Error  string
}

type UpdateUserOnlineOk struct {
	Status bool
	Action string
}
type UpdateUserOnlineErr struct {
	Status bool
	Error  string
}
type GetAllUsersWithOmlineResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Online        []bool
	Users         []*models.User
}
type GetAllUsersWithOmlineResponseErr struct {
	Status bool
	Error  string
}
type GetUsersWithLastMsgsResponseOk struct {
	Status        bool
	Action        string
	IsTokenExpire bool
	Online        []bool
	Users         []*models.User
	LastMsgs      []*models.Message
}
type GetUsersWithLastMsgsResponseErr struct {
	Status bool
	Error  string
}
