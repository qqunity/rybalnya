package models

import "time"

type Message struct {
	tableName struct{} `pg:"messages"`
	ID        uint64   `pg:",pk"`
	UserID    uint64
	ChatID    uint64
	Message   string
	IsEdited  bool      `pg:"default:false"`
	IsSeen    bool      `pg:"default:false"`
	CreatedAt time.Time `pg:"default:now()"`
	User      *User     `pg:"rel:has-one" json:"-"`
	Chat      *Chat     `pg:"rel:has-one" json:"-"`
}
