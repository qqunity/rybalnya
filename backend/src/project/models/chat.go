package models

type Chat struct {
	tableName struct{} `pg:"chats"`
	ID        uint64   `pg:",pk"`
	Roomname  string
	Messages  []*Message `pg:"rel:has-many" json:"-"`
}
