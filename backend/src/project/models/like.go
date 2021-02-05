package models

type Like struct {
	tableName struct{} `pg:"likes"`
	UserID    uint64   `pg:",pk"`
	PostID    uint64   `pg:",pk"`
	User      *User    `pg:"rel:has-one" json:"-"`
	Post      *Post    `pg:"rel:has-one" json:"-"`
}
