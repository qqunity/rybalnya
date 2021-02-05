package models

import (
	"fmt"
)

type User struct {
	tableName struct{} `pg:"users"`
	ID        uint64   `pg:",pk"`
	FullName  string
	Nickname  string
	Email     string `pg:",unique,notnull"`
	Password  string
	About     string
	Image     []byte
	Maps      []*MapInfo `pg:"rel:has-many" json:"-"`
	Messages  []*Message `pg:"rel:has-many" json:"-"`
	Posts     []*Post    `pg:"rel:has-many" json:"-"`
	Comments  []*Comment `pg:"rel:has-many" json:"-"`
}

func (u *User) String() string {
	return fmt.Sprintf("User<%d \t %s \t %s \t %s \t %s>", u.ID, u.FullName, u.Nickname, u.Email, u.Password)
}
