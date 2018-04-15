package com.example.vvoitsekh.googlemapskyivsights

import javax.inject.Inject

/**
 * Created by Vlad on 14.04.2018.
 */
class PlacesRepository @Inject constructor(){
    private var showplaces: List<Showplace>
    init {
        showplaces = populateRepository()
    }

    fun getPlaces() = showplaces

    private fun populateRepository(): List<Showplace> {
        val showplacesList = ArrayList<Showplace>()
        showplacesList.add(Showplace("St Sophia's Cathedral", 50.452778, 30.514444))
        showplacesList.add(Showplace("Kyevo-Pecherska Lavra", 50.434167, 30.559167))
        showplacesList.add(Showplace("Maydan Nezalezhnosti", 50.45, 30.524167))
        showplacesList.add(Showplace("St Michael's Golden-Domed Monastery", 50.455556, 30.522778))
        showplacesList.add(Showplace("PinchukArtCentre", 50.441696, 30.521141))
        showplacesList.add(Showplace("Park of Eternal Glory", 50.438889, 30.554444))
        showplacesList.add(Showplace("Khreshchatyk", 50.452222, 30.527222))
        showplacesList.add(Showplace("Andriyivsky Uzviz", 50.458361, 30.518222))
        showplacesList.add(Showplace("St Andrew's Church", 50.458889, 30.518056))
        showplacesList.add(Showplace("House of Chimeras", 50.445, 30.528611))
        showplacesList.add(Showplace("National Museum of Ukrainian History", 50.458333, 30.516111))
        showplacesList.add(Showplace("Zoloti Vorota", 50.448889, 30.513333))
        showplacesList.add(Showplace("National Art Museum", 50.449444, 30.531111))
        showplacesList.add(Showplace("Museum of Microminiature", 50.43435, 30.55665))
        showplacesList.add(Showplace("St Volodymyr's Cathedral", 50.444722, 30.508889))
        showplacesList.add(Showplace("Peyzazhna aleya", 50.4575, 30.515472))
        showplacesList.add(Showplace("Friendship of Nations Monument", 50.454444, 30.53))
        showplacesList.add(Showplace("Water Museum", 50.452639, 30.531583))
        showplacesList.add(Showplace("Chocolate House", 50.443183, 30.531483))
        showplacesList.add(Showplace("St Nicholas Naberezhny", 50.467389, 30.52325))
        showplacesList.add(Showplace("Kyiv Mohyla Academy", 50.464443, 30.519816))
        showplacesList.add(Showplace("Prince Volodymyr the Great Monument", 50.4564, 30.5263))
        showplacesList.add(Showplace("Mariyinskiy Palace", 50.448333, 30.5375))
        showplacesList.add(Showplace("Kyiv River station", 50.4584, 30.5338))
        showplacesList.add(Showplace("Park and Monument to Taras Shevchenko", 50.44173, 30.512971))
        showplacesList.add(Showplace("Monument to the Founders of Kyiv", 50.429092, 30.568919))
        showplacesList.add(Showplace("Monument to Bohdan Khmelnytstsky", 50.45356, 30.51651))
        showplacesList.add(Showplace("Square of Contracts", 50.463889, 30.518056))
        showplacesList.add(Showplace("Samson Fountain", 50.464167, 30.516944))
        showplacesList.add(Showplace("Taras Shevchenko National Opera of Ukraine", 50.446667, 30.5125))
        showplacesList.add(Showplace("Candle of Memory monument", 50.4382, 30.554))
        showplacesList.add(Showplace("Askold Grave Park", 50.44369, 30.55311))
        showplacesList.add(Showplace("Museum of One Street", 50.4617, 30.5209))
        showplacesList.add(Showplace("Olympic Stadium", 50.433411, 30.521844))
        showplacesList.add(Showplace("Mystetskyi Arsenal art quarter", 50.434167, 30.553333))
        showplacesList.add(Showplace("Saint Nicholas Roman Catholic Cathedral", 50.426852, 30.517638))
        showplacesList.add(Showplace("Kiev Academic Puppet Theatre", 50.451944, 30.530556))
        showplacesList.add(Showplace("National Bank of Ukraine", 50.446944, 30.531944))
        showplacesList.add(Showplace("Kiev Funicular", 50.4575, 30.523333))
        showplacesList.add(Showplace("Museum of Western and Oriental Art", 50.441111, 30.514444))
        showplacesList.add(Showplace("Vozdvyzhenska", 50.46055, 30.51147))
        showplacesList.add(Showplace("House of the Weeping Widow", 50.443611, 30.527778))
        showplacesList.add(Showplace("Kiev Zoo", 50.454167, 30.462778))
        showplacesList.add(Showplace("Igor Sikorsky Kyiv Polytechnic Institute", 50.44952, 30.46255))
        showplacesList.add(Showplace("Kiev National Academic Theatre of Operetta", 50.433539, 30.516278))
        showplacesList.add(Showplace("Yaroslaviv Val street", 50.454361, 30.505861))
        showplacesList.add(Showplace("Velyka Zhytomyrska street", 50.45525, 30.519806))
        showplacesList.add(Showplace("Park Bridge across the Dnipro", 50.456778, 30.534389))
        showplacesList.add(Showplace("Pharmacy Museum in Kyiv", 50.464028, 30.514167))
        showplacesList.add(Showplace("Teacher's House in Kyiv", 50.444758, 30.513512))
        return showplacesList
    }
}