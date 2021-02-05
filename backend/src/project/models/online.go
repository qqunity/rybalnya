package models

type Online struct {
	tableName struct{} `pg:"online"`
	UserID    uint64   `pg:",pk"`
	IsOnline  bool     `pg:"default:true"`
	User      *User    `pg:"rel:has-one" json:"-"`
}
