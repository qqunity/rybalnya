package models

import "time"

type MapInfo struct {
	tableName struct{} `pg:"map_info"`
	ID        int64    `pg:",pk"`
	Info      string
	Image     []byte
	Lat       float64
	Long      float64
	UserID    int64
	User      *User     `pg:"rel:has-one" json:"-"`
	CreatedAt time.Time `pg:"default:now()"`
}
