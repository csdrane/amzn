g = new Dygraph(

    // containing div
    document.getElementById("graphdiv"),

    // CSV or path to a CSV file.
    "/csv/" + document.getElementById("graphdiv").getAttribute("productid")

);
