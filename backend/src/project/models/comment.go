package models

import "time"

type Comment struct {
	tableName  struct{} `pg:"comments"`
	ID         uint64   `pg:",pk"`
	UserID     uint64
	PostID     uint64
	Content    string
	WasUpdated bool      `pg:"default:false"`
	CreatedAt  time.Time `pg:"default:now()"`
	User       *User     `pg:"rel:has-one" json:"-"`
	Post       *Post     `pg:"rel:has-one" json:"-"`
}
