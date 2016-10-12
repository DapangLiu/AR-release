
# Alien Where

Team Members: Ruibo Liu & Tian Xia

![](https://s3.amazonaws.com/artagfinal/signin1.png)

## Goals 

Our goal is to build a application which meets following requirements:

1. Swift development based on appropriate __Modularity__, including needed __Abstraction__ and [MVC](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller) project structure.
2.  Friendly UI and smooth inter-activity transitions.
3.  Valid API and GMS availability check (at runtime) mechanism avoiding bad crash errors. Lower API support, which means high level devices adaptability.
4.  Enhance the accuracy of “tag recognition” through Machine Learning integration.
5. Social Network features. Enable users share their tags to nearby users or users at other places.

In terms of above requirements, this project could be restated in following way:

The app (We name it with “Alien Where”)’s background is a battle where the aliens from different planets invading the earth are trying to conquer as many as places with “placing” tags, and as an alien from certain planet, you need to “collet” tags placed by aliens from other planets. So, the more tags you place, the more difficulties caused by you for others to conquer the earth completely, and the more tags you collect, the higher score you have and your alliance would ranks higher.



## Design

Currently, we have developed 3 of 4 basic functions (Place, Gallery, Score) and Collect is under heavy development. One more thing is we still have some problems of fetching data from server through “findtag.php”. But I think I would make it given several days.

![](https://s3.amazonaws.com/artagfinal/AR+game.png)

Above is the project structure in high level. All activities are marked with circles and classes marked with rounded rectangles.


### Neat, because of Module Isolation

For the goal mentioned as the 1st point of previous part, I reorganized all of the project files in a MVP way, which means the isolation between data processing and UI part. You can see my effort through following capture.

 ![](https://s3.amazonaws.com/agwarbliu/SCREENSHOT.png)

All the activities are contained in the “UI” package. All needed data processing are rearranged into different sub-packages in “Data” package. What’s more, the Google Plus Service Check and Permission Check, two core classes for required pre-check, are extracted as two helper classes so that every activity could call them in a more neat way.

### Deliberate, at details you may Ignore

Never stop thinking to optimize our app for a better using experience. We chose to create our own menu bar button instead of copying the API default "My Current Location" button pattern. We know that a fully immersing environment and clean map view would be our key advantage compared to similar products.

![](https://s3.amazonaws.com/agwarbliu/newlocation.jpg)

We even consider about how to help our users to save energy when using GPS service. We "remove the location update" after certain interval so that it is intelligent enough to save the valuable battery energy for our users.

```java
    ...
    @Override
    protected void onPause() {
        super.onPause();
        if (mMapPermissionResult) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mLocationClient, mListener
            );
        }
    }
    ...
```
### Graceful, but not Redundant

The user interface would be definitely ugly if we only focus on the code itself. So in our project, we deploy a floating button menu as the crucial interface in the Map Activity. Only if the user want to go ahead, the further functions buttons would show up (with clicking the menu button). So in the case when our customers just wander around in the Map, it would be comfort for him/her to read the map without any ugly buttons blocking.

![](https://s3.amazonaws.com/agwarbliu/Button.png)	 

Another detail is about variety of permission and Google service check. It’s a headache part because you have to handle many cases: for example, when the device is not installed the Google Play Service, what should you do? You ask the user give the “Camera” permission through a dialog, but the user click “Deny” by mistake… How would you give our user another chance to grant?

A rude requirement is not allowed in our project. We rearrange the whole requirement sequence and consider about every possible case. What you would see is totally friendly but valid enough permission request mechanism.

![](https://s3.amazonaws.com/agwarbliu/Permission1.png)	 

“1 of 3” means our app needs three permissions and our users would decide such things when first time getting into the Map Activity. After the first time, if our user grant all of three permissions, there is no need for deciding another time, because our app would listen to the click result and set all things in background.

If our user is too careful to grant our required permissions, the app would popup a info dialog to inform our users of why we need such permissions. It looks like:



Of course we still have a long way to optimize the user interface. But I think currently above is a good try.

### Well-Organized Process, Logical and Friendly

We think that nowadays users prefer a better experience when clicking and switching from one activity to another. Thus, building a logical process is the key point when we developed the collecting tag part.

First, our users would face a familiar map view when click “Collect Button” in the Map Activity. We add the half-transparent “Show My Location” button instead of the custom “Current Location Menu Button” to provide space for the “Show Nearby Tags Menu Button”. And the new story should be: Our users first click the current location button to locate themselves and then click the show nearby tags button to see available nearby tags.

![](https://s3.amazonaws.com/artagfinal/nearby_fin.jpg)	

Great! When having decided which tag to collect, our users may click the marker and a cute marker info window would pump up. The info window shows the tag’s azimuth value, altitude value and tag ID. To collect it, users just need click the window.

Then a graceful intro shows up. The focus circle notifies our users how to step to the collecting tags camera view. The preview of tag is needed because only if our users know what it’s like, they could draw a similar tag in the next step. 

![](https://s3.amazonaws.com/artagfinal/preview_fin.jpg)

### Social Network, a Great Idea (Extra Credits)

I think this is would be the bonus part which is not included by the assignment itself. Does not it weird if we use users’ google account to login but not use this account to do some share?

So in our app we integrate Google Plus Service, so that in the Gallery Activity, our users could share the tags with their friends. 

![](https://s3.amazonaws.com/artagfinal/signin_fin.jpg)

The required permission scope would be notified to our users when they login their Google Plus account.

 ![](https://s3.amazonaws.com/agwarbliu/Share.png)

__ATTENTION!__: Currently I did not implement the "collect" part so I have no idea how to get a galley list of “collected tags”. It would be empty list if I call the gallery.php right now. Alternatively, I used Easy.com’s API to show the demo version of Gallery Activity.

### Interfaces

A good abstraction is crucial for the application. So in previous part, I mentioned about the Permission Check Helper class and Google Service Check Helper class.

I would just introduce the Google Service Check Helper class in brief.

First I defined two interface.
```java
	public interface GMSListener {

	    public void onConnected();

	    public void onDisconnected();
	}
```
And this interface is implemented by ListingAdapter.class:
```java
	public class ListingAdapter extends RecyclerView.Adapter<ListingHolder>
	                            implements Callback<ActiveListings>,
	                                        GooglePlusServiceHelper.GMSListener {
	…}
```
The class has to implement these two methods:
```java
	@Override
	public void onConnected() {

	    // No matter which case, we should load the data
	    if(getItemCount() == 0) {

	        Etsy.getActiveListing(this);
	    }

	    isGMSAvailable = true;

	    // Would make the view redraw
	    notifyDataSetChanged();
	}

	@Override
	public void onDisconnected() 

	    if(getItemCount() == 0) {

	        Etsy.getActiveListing(this);
	    

	    isGMSAvailable = false;

	    // Would make the view redraw
	    notifyDataSetChanged();

	}
```
The “isGMSAvailable” would act as a flag to tell the Gallery Activity whether we could display the “share button” and “plus one button” in the view.
```java
	@Override
	public void onBindViewHolder(ListingHolder holder, int position) {
	    final Listing listing = mActiveListings.results[position];
	    holder.mTitleView.setText(listing.title);
	    holder.mShopView.setText(listing.Shop.shop_name);
	    holder.mPriceView.setText(listing.price);

	    // Use Picasso to load Image for the Holder
	    Picasso.with(holder.mImageView.getContext())
	            .load(listing.Images[0].url_570xN)
	            .into(holder.mImageView);


	    // Decide whether we could show the share button and the +1 button
	    if(isGMSAvailable) {
	        holder.mPlusOneButton.setVisibility(View.VISIBLE);
	        holder.mPlusOneButton.initialize(listing.url, RES_CODE_PLUS_ONE);
	        holder.mPlusOneButton.setAnnotation(PlusOneButton.ANNOTATION_BUBBLE);

	        holder.itemView.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                Intent openListingUrl = new Intent(Intent.ACTION_VIEW);
	                openListingUrl.setData(Uri.parse(listing.url));
	                mActivity.startActivity(openListingUrl);
	            }
	        });

	        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                Intent intent = new PlusShare.Builder(mActivity)
	                        .setType("text/plain")
	                        .setText("Checkout this item on Etsy "+ listing.title)
	                        .setContentUrl(Uri.parse(listing.url))
	                        .getIntent();

	                mActivity.startActivityForResult(intent, RES_CODE_SHARE);

	            }
	        });


	    } else {
	        holder.mPlusOneButton.setVisibility(View.GONE);

	        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                Intent intent = new Intent(Intent.ACTION_SEND);
	                intent.putExtra(Intent.EXTRA_TEXT, "Check out this item on Etsy " +
	                listing.title + "" + listing.url);
	                intent.setType("text/plain");

	                mActivity.startActivityForResult(Intent.createChooser(intent, "Share"), RES_CODE_SHARE);
	            }
	        });

	    }

	}
```  
In a word, no matter in which case (GMS installed or not), our users would see the gallery list, and the only difference is: whether they would see two social media buttons provided by Google Plus.



## Strategy

My teammate and I have clear goals and organized schedule. I’m responsible for the code part and my teammate’s previous major is Architecture so he had great experience of design. All the previous stage app mockups were made by him.

Because I have some experience of development so I schedule the whole process. We made use of [Trello](https://trello.com/) to help us arrange the whole project timing.



## Challenges

### Permission Check at Runtime

It is definitely the most difficult part for me and it takes me a lot of time.

After API 23 Google requires the developers to request permissions at run time, because it would clean up the install process without asking users for permissions again and again. (Do you still remember the permission check boxes before?)

Such requirement is founded when I developed the camera part. Android Studio asked me to require permission at runtime, or I would not pass the build. I read through all the docs or stack overflow posts and I found nearly nobody could provide a abstractive and valid enough way to do such check. 

I read [Google Developer Docs](https://developer.android.com/training/permissions/requesting.html) about that. You could compare it with my final code implement, and you would know it provided very limited demo for such function. It does not tell us when to call these methods during Activity LifeCycle, and does not show how to handle listener result of users’ permission grant to deny click. All the things need to be completed by ourselves.

The worse thing is, when I implemented the LOCATION permission check, I found I needed two more checks because the camera part required (CAMERA, WRITE\_EXTERNAL\_STORAGE). So how to show all the permission request dialogs gracefully and handle all the results?

I stated to look through the source code of ActivityCompat.class and figure out how the static requestPermissions() method works. Finally, I made it! Very funny part is that the core code of this function is just:
```java
	public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
	                                          String permission) {
	    for (int i = 0; i < grantPermissions.length; i++) {
	        if (permission.equals(grantPermissions[i])) {
	            return grantResults[i] == PackageManager.PERMISSION_GRANTED;
	        }
	    }
	    return false;
	}
```
It looks like every one could do the permissions search part, and indeed there is no difference between this one and Java String compare. But this challenge tells me that, sometimes the wheel has been built, and what you should do is just to find it and not give up.

### Misstep and Misunderstanding

I talked with professor many times about the nearby tags. At first I think there is no need to submit the users’ email when showing the nearby tags because it should have had links only with the location. But actually, the nearby tags could only show the tags you’ve not collected. In other words, the tags are stored in separate databases: one is collected and the other is uncollected. Such fact is the misunderstanding till today.

And I think the problem is very obvious. If I do not implement place tags function, I would not have any uncollected tags shown in my gallery, which means I could not do any debug of my gallery part only after I do the place part. I think it would bring about some difficulties for the developers.



## How to Improve?

1. The UI part still need polish. 
2. I would recode the camera part because camera API v2 has been released. The reason why I did not use it currently is the new API needs Android Version Check. In other words, it could not adapt to old android devices.
3. Do more things about lifecycle control and energy management.

I think this project is really a good sample for essential Android training. The stuff about Fragment and Activity, RecyclerView and Floating Button are all important things in nowadays development. 


## Schedule

Below is out project management tool: Trello.

 ![](https://s3.amazonaws.com/agwarbliu/schedulenew.png)
 
We cut the process into three parts: To do, Doing and Done. We would focus on the todo part first, and then moving some of them into doing column. When completed, it would be moved into done column. In this way, my team member and I could share the progress in cloud and keep everything works well.
 
 Our schedule is well-organized. Different parts have distinguished colors. And due data is attached to every item.


## Extra Credits

I think I've done some parts beyond the requirement. For example, the Google Plus Share and Plus One part added social media element into our app. What's more, things like power saving, persmissions request at run time... I've got no idea whether such stuff should be part of the "required part", but in my standard, they are needed, and I made it, which I think could be the reasons for extra credits.

For suggestion about AGWA API, I think the most important part to improve is the data structure. I think the String key and value pair.. yes it works. But for large quantities of data, for example, when tags become more and more, I think it would be better to use JSON Object --- for example, JSON Array to store the tags and correlative location info, user email and other stuff. It's a more friendly way to users and more wide-used method in server backend nowadays. 

That's all. Really thanks professor. It’s a great experience.


by Ruibo
  



