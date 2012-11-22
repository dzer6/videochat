get  "/evercookie_index.php", forward: "/evercookie_index.groovy"
get  "/evercookie_cache.php", forward: "/evercookie_cache.groovy"
get  "/evercookie_etag.php", forward: "/evercookie_etag.groovy"
get  "/evercookie_png.php", forward: "/evercookie_png.groovy"

post "/home/block", forward: "/home_block.groovy"
post "/home/cusp", forward: "/home_cusp.groovy"
post "/home/ec", forward: "/home_ec.groovy"
get  "/home/index", forward: "/home_index.groovy"
post "/home/next", forward: "/home_next.groovy"
post "/home/play", forward: "/home_play.groovy"
post "/home/smtc", forward: "/home_smtc.groovy"
post "/home/stop", forward: "/home_stop.groovy"
post "/home/bannedtill", forward: "/home_bannedtill.groovy"

get  "/", forward: "/home_index.groovy"
get  "", forward: "/home_index.groovy"

all "/monitoring/**", ignore: true
all "/monitoring**", ignore: true

get  "/favicon.ico", redirect: "/images/ch.png"