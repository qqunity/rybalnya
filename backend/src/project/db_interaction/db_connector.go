package db_interaction

import (
	"fmt"

	"github.com/go-pg/pg/v10"
	"github.com/go-pg/pg/v10/orm"
	"github.com/my/repo/backend/src/project/models"
)

func DBConnetor(isDBCreated bool) *pg.DB {
	db := pg.Connect(&pg.Options{
		Addr:     "37.230.114.186:5432",
		User:     "admin",
		Password: "rybalnya2020",
		Database: "rybalnyadb",
	})
	if !isDBCreated {
		err := createSchema(db)
		if err != nil {
			panic(fmt.Errorf("FAIL. Fatal error with db connector: %s", err))
		}
	}
	return db
}

func createSchema(db *pg.DB) error {
	models := []interface{}{
		(*models.Online)(nil),
	}

	for _, model := range models {
		err := db.Model(model).CreateTable(&orm.CreateTableOptions{
			Temp: false,
		})
		if err != nil {
			return err
		}
	}
	return nil
}
