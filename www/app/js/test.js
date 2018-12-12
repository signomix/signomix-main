        try{
            map = L.map(opts.ref+'_m')
        }catch(err){
            console.log(err)
        }
        map.setView([self.lat, self.lon], zoom)
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        
        try{
            marker.setLatLng([self.lat, self.lon])
            marker.setPopupContent(self.lat+','+self.lon)
        }catch(err){
            marker=L.marker([self.lat, self.lon])
            marker.addTo(map).bindPopup(self.lat+','+self.lon)
        }
        
        if(self.jsonData.length>1){
            var latlngs =[]
            var polyline
            for(i=0; i<self.jsonData.length; i++){
                if(latFirst){
                    latlngs.push(
                        [
                        parseFloat(self.jsonData[i][1]['value']),
                        parseFloat(self.jsonData[i][0]['value'])
                        ]
                    )
                }else{
                    latlngs.push(
                        [
                        parseFloat(self.jsonData[i][0]['value']),
                        parseFloat(self.jsonData[i][1]['value'])
                        ]
                    )
                }
            }
            console.log(latlngs)
            polyline = L.polyline(latlngs, {
                color: 'red'
            }).addTo(map);
            // zoom the map to the polyline
            map.fitBounds(polyline.getBounds());
        }

