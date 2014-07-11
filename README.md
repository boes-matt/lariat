# Lariat Places

Lariat Places is a silly name for an Android app that demonstrates implementation, testing, and use of a custom content provider through a cursor loader.

## Things to note

[LariatProviderTest]() shows you how the content provider should work.
[LariatProvider]() implements the CRUD operations using SQLite, including performing joins on queries (see code using projection map).

[PlaceCursor]() and [UserCursor]() make it easy to read data from the cursor without worrying about column names.

[MainActivity]() lets you randomly add or delete places of the Wild West.
Watch how the data cursor held by the adapter auto updates the list view when you add or delete a place.
This works because a notification uri is set on the cursor before it's returned from the provider on a query, and anytime the provider performs a side effect, it notifies potentially affected uris of the change.  See [LariatProvider]().

## Obligatory screenshot

<img src="https://raw.githubusercontent.com/boes-matt/lariat/master/app/screenshots/wildwest.png" height="375" />
