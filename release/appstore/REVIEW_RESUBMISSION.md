# Shareat 1.0: resubmission after Apple's information request

Apple's 25 September 2026 message asks for evidence and six answers under Guideline 2.1.
Use the **final installed build** for the recording and the same description in both the
App Review reply and the version's App Review Information → Notes. Do not submit
the old build 1.0 (3) after the code and database changes in this branch.

## Before recording

1. Merge this branch, apply both new Supabase migrations to production, and upload
   a new iOS build. Verify that RevenueCat initializes in the launch build, while
   Shareat Unlimited has no visible entry point, paid benefit, or purchase flow.
   Keep the planned IAP products in draft until the benefits are implemented.
2. Create a restaurant demo account with a **published** restaurant, a menu,
   dishes, prices, declared allergens, and photos you have permission to use.
3. Create a customer demo account. For report/block, create another customer
   account and leave an approved sample comment on a published dish. One account
   cannot report or block its own review. Keep these accounts active for Apple.
4. Review the new pending comments and reports at least daily; see below.
5. Test registration, login, browsing, restaurant management, review submission,
   reporting, blocking, and account deletion request on a physical supported
   iPhone or iPad with the latest available OS. The deletion request is processed
   manually within 30 days; it does not delete immediately.

## Physical-device recording

Start the iOS screen recording before tapping the Shareat icon. Show a typical
journey: browse without signing in; register or log in as a customer; open a
restaurant and menu; leave a star rating and comment (comments wait for
moderation); report another customer's approved comment; block that customer
and show the comment disappears; open Settings and submit an account deletion
request **using a disposable account**; log in as the restaurant and show menu
management. Do not delete the reusable reviewer demo account. No paid-feature
flow should appear in this launch build.

## App Review answer outline

1. **Purpose and audience:** Shareat helps people in Spain choose restaurant
   dishes using menus, prices, declared allergens, and dish-level reviews. It
   lets restaurants publish and manage their own menus.
2. **Access:** Guests browse published restaurants. Customer and restaurant
   accounts use email/password. Put working customer and restaurant credentials
   in App Review Information; add the second customer account used for
   report/block to Notes. Tell Apple which sample restaurant and dish to open.
3. **Services:** Supabase hosts authentication, account data, menus, reviews,
   and uploaded images. RevenueCat is integrated for a future Unlimited release,
   but no paid content or purchase flow is enabled in this launch build.
4. **Regions:** This release is distributed only in Spain; its available
   features are consistent across Spain.
5. **Rights/regulation:** Shareat is a restaurant discovery/review app, not a
   regulated medical, financial, or gambling service. Only state that the app
   may display restaurant photos and menus when you have the necessary rights.
   Attach licenses or restaurant consent if Apple asks for them; remove any
   uncertain material instead of claiming permission you cannot verify.
6. **Recording:** Attach the physical-device recording to the reply. State the
   build number, device model, and OS version used.

## Daily moderation in Supabase

Use the production project's SQL Editor with an owner account. Do not run these
queries with an app client key. Review reports promptly and decide whether to
approve, remove, or dismiss them. Record decisions before marking a report
resolved. The comments queue is:

```sql
select id, author_account_id, comment, created_at
from public.reviews
where moderation_status = 'hidden' and comment is not null
order by created_at;
```

The reports queue is:

```sql
select rr.id, rr.review_id, rr.reason, rr.created_at, r.comment
from private.review_reports rr
join public.reviews r on r.id = rr.review_id
where rr.status = 'open'
order by rr.created_at;
```

After examining a specific review ID, approve a safe comment with
`update public.reviews set moderation_status = 'visible' where id = '<id>' and moderation_status = 'hidden';`
or remove abusive content with
`update public.reviews set moderation_status = 'removed' where id = '<id>';`.
For a resolved report, update that report's `status` to `resolved` or
`dismissed`. Keep the contact address on the support page working and respond
to reports promptly.
