package models

import "time"

type Post struct {
	tableName   struct{} `pg:"posts"`
	ID          uint64   `pg:",pk"`
	Image       []byte
	Description string
	UserID      uint64
	CreatedAt   time.Time  `pg:"default:now()"`
	User        *User      `pg:"rel:has-one" json:"-"`
	Likes       []*Like    `pg:"rel:has-many" json:"-"`
	Comments    []*Comment `pg:"rel:has-many" json:"-"`
}
