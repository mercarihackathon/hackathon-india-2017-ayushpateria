package main

import (
    "encoding/json"
    "fmt"
    "sort"
    "log"
    "net/http"
    "strings"
    "strconv"
    "math"
    "goji.io"
    "goji.io/pat"
    "gopkg.in/mgo.v2"
    "gopkg.in/mgo.v2/bson"
)

func ErrorWithJSON(w http.ResponseWriter, message string, code int) {  
    w.Header().Set("Content-Type", "application/json; charset=utf-8")
    w.WriteHeader(code)
    fmt.Fprintf(w, "{message: %q}", message)
}

func ResponseWithJSON(w http.ResponseWriter, json []byte, code int) {  
    w.Header().Set("Content-Type", "application/json; charset=utf-8")
    w.WriteHeader(code)
    w.Write(json)
}

type Spot struct {  
    Title    string   `json:"title"`
    Cost   string   `json:"cost"`
	User string `json:"user"`
	Addr	string `json:"address"`
	Coord   string   `json:"coord"`
	Distance  string  `json:"distance,omitempty"`
}

func main() {  
    session, err := mgo.Dial("localhost")
    if err != nil {
        panic(err)
    }
    defer session.Close()

    session.SetMode(mgo.Monotonic, true)
    ensureIndex(session)

    mux := goji.NewMux()
    mux.HandleFunc(pat.Get("/spots"), allSpots(session))
    mux.HandleFunc(pat.Post("/spots"), addSpot(session))
    mux.HandleFunc(pat.Get("/spots/:coord"), spotByCoord(session))
    mux.HandleFunc(pat.Put("/spots/:coord"), updateSpot(session))
    mux.HandleFunc(pat.Delete("/spots/:coord"), deleteSpot(session))
    http.ListenAndServe("0.0.0.0:80", mux)
}

func ensureIndex(s *mgo.Session) {  
    session := s.Copy()
    defer session.Close()

    c := session.DB("smartpark").C("spots")

    index := mgo.Index{
        Key:        []string{"coord"},
        Unique:     true,
        DropDups:   true,
        Background: true,
        Sparse:     true,
    }
    err := c.EnsureIndex(index)
    if err != nil {
        panic(err)
    }
}

func allSpots(s *mgo.Session) func(w http.ResponseWriter, r *http.Request) {  
    return func(w http.ResponseWriter, r *http.Request) {
        session := s.Copy()
        defer session.Close()

        c := session.DB("smartpark").C("spots")

        var spots []Spot
        err := c.Find(bson.M{}).All(&spots)
        if err != nil {
            ErrorWithJSON(w, "Database error", http.StatusInternalServerError)
            log.Println("Failed get all spots: ", err)
            return
        }

        respBody, err := json.MarshalIndent(spots, "", "  ")
        if err != nil {
            log.Fatal(err)
        }

        ResponseWithJSON(w, respBody, http.StatusOK)
    }
}

func addSpot(s *mgo.Session) func(w http.ResponseWriter, r *http.Request) {  
    return func(w http.ResponseWriter, r *http.Request) {
        session := s.Copy()
        defer session.Close()

        var spot Spot
        decoder := json.NewDecoder(r.Body)
        err := decoder.Decode(&spot)
        if err != nil {
            ErrorWithJSON(w, "Incorrect body", http.StatusBadRequest)
            return
        }

        c := session.DB("smartpark").C("spots")

        err = c.Insert(spot)
        if err != nil {
            if mgo.IsDup(err) {
                ErrorWithJSON(w, "Spot with this Coordinate already exists", http.StatusBadRequest)
                return
            }

            ErrorWithJSON(w, "Database error", http.StatusInternalServerError)
            log.Println("Failed insert spot: ", err)
            return
        }

        w.Header().Set("Content-Type", "application/json")
        w.Header().Set("Location", r.URL.Path+"/"+spot.Coord)
        w.WriteHeader(http.StatusCreated)
    }
}

func spotByCoord(s *mgo.Session) func(w http.ResponseWriter, r *http.Request) {  
    return func(w http.ResponseWriter, r *http.Request) {
        session := s.Copy()
        defer session.Close()

        coord := pat.Param(r, "coord")
	fmt.Println(coord)
        c := session.DB("smartpark").C("spots")

        var spots []Spot
        err := c.Find(bson.M{}).All(&spots)
        if err != nil {
            ErrorWithJSON(w, "Database error", http.StatusInternalServerError)
            log.Println("Failed get all spots: ", err)
            return
        }

	sort.Slice(spots, func(i, j int) bool {
	di := Distance(spots[i].Coord, coord)
	dj := Distance(spots[j].Coord, coord)
	spots[i].Distance =  fmt.Sprintf("%.1f", di)
	spots[j].Distance =  fmt.Sprintf("%.1f", dj) 
	return di < dj
	})

	lim := r.URL.Query().Get("limit")
	if lim != "" {
	lmt ,_ := strconv.ParseInt(lim, 10, 0)
	if(int(lmt) < len(spots)){		
		spots = spots[:lmt]
	}
	}
	respBody, err := json.MarshalIndent(spots, "", "  ")
        if err != nil {
            log.Fatal(err)
        }

        ResponseWithJSON(w, respBody, http.StatusOK)
    }
}

func updateSpot(s *mgo.Session) func(w http.ResponseWriter, r *http.Request) {  
    return func(w http.ResponseWriter, r *http.Request) {
        session := s.Copy()
        defer session.Close()

        coord := pat.Param(r, "coord")

        var spot Spot
        decoder := json.NewDecoder(r.Body)
        err := decoder.Decode(&spot)
        if err != nil {
            ErrorWithJSON(w, "Incorrect body", http.StatusBadRequest)
            return
        }

        c := session.DB("store").C("spots")

        err = c.Update(bson.M{"coord": coord}, &spot)
        if err != nil {
            switch err {
            default:
                ErrorWithJSON(w, "Database error", http.StatusInternalServerError)
                log.Println("Failed update spot: ", err)
                return
            case mgo.ErrNotFound:
                ErrorWithJSON(w, "Spot not found", http.StatusNotFound)
                return
            }
        }

        w.WriteHeader(http.StatusNoContent)
    }
}

func deleteSpot(s *mgo.Session) func(w http.ResponseWriter, r *http.Request) {  
    return func(w http.ResponseWriter, r *http.Request) {
        session := s.Copy()
        defer session.Close()

        coord := pat.Param(r, "coord")

        c := session.DB("store").C("spots")

        err := c.Remove(bson.M{"coord": coord})
        if err != nil {
            switch err {
            default:
                ErrorWithJSON(w, "Database error", http.StatusInternalServerError)
                log.Println("Failed delete spot: ", err)
                return
            case mgo.ErrNotFound:
                ErrorWithJSON(w, "Spot not found", http.StatusNotFound)
                return
            }
        }

        w.WriteHeader(http.StatusNoContent)
    }
}

func hsin(theta float64) float64 {
	return math.Pow(math.Sin(theta/2), 2)
}

func Distance(c1, c2 string) float64 {
	// convert to radians
  	// must cast radius as float to multiply later
	cord1 := strings.Split(c1, ",")
	cord2 := strings.Split(c2, ",")
	var lat1, lon1, lat2, lon2, la1, lo1, la2, lo2, r float64
	lat1,_ = strconv.ParseFloat(cord1[0], 64)
	lat2,_ = strconv.ParseFloat(cord2[0], 64)
	lon1,_ = strconv.ParseFloat(cord1[1], 64)
	lon2,_ = strconv.ParseFloat(cord2[1], 64)	
	la1 = lat1 * math.Pi / 180
	lo1 = lon1 * math.Pi / 180
	la2 = lat2 * math.Pi / 180
	lo2 = lon2 * math.Pi / 180

	r = 6378100 // Earth radius in METERS

	// calculate
	h := hsin(la2-la1) + math.Cos(la1)*math.Cos(la2)*hsin(lo2-lo1)

	return 2 * r * math.Asin(math.Sqrt(h))/1000
}
