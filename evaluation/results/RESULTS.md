# Captured measurement run

Produced by `python3 evaluation/lib/measure.py` and `./evaluation/check-compilability.sh` on 2026-08-08.
Every value derives from artefacts committed to this repository; nothing contacts a network.

## M1, M2, M3, M9

```
==============================================================================
P1 — SauceDemo inventory page. All values derived from committed artefacts.
==============================================================================

--- M1  Ground truth, counted on the DOM snapshot ---
  whole page : {'input': 0, 'button': 8, 'a': 20, 'select': 1}  -> 29 interactive elements
  <header>   : ABSENT — no header class is generated
  <footer>   : {'input': 0, 'button': 0, 'a': 3, 'select': 0}
  body scope : {'input': 0, 'button': 8, 'a': 17, 'select': 1}  -> 26 interactive, 25 of the kinds the parser recognises

--- M9  Determinism (md5 after replacing the class name with 'X') ---
  Pipeline A: 5 runs of the same page, 1 distinct digest(s) — BYTE-IDENTICAL
      file                                         md5 raw                           md5 class-name normalised         lines  By  members  package
      SauceDemOInventoryPagePIPABody.java          6e7cc9025e10604d117e7da7c0eef523  420c015aece99c3da5f12330a49934a0  93     3   14       pages.generated
      SauceDemoInventoryPagePIPBBody.java          eee5978925e28de70e726f1ad57b9481  420c015aece99c3da5f12330a49934a0  93     3   14       pages.generated
      SauceDemoInventoryPagePIPBBodyRun2Body.java  bc3c11a649bda7ef3b4fadf9663a7b9a  420c015aece99c3da5f12330a49934a0  93     3   14       pages.generated
      SauceDemoInventoryPagePIPBBodyRun3Body.java  b10f38db2e695db3eb62a26ab915a74b  420c015aece99c3da5f12330a49934a0  93     3   14       pages.generated
      testinglOGINBody.java                        1a9eb85829d2b4863a880ec9e88b07a2  420c015aece99c3da5f12330a49934a0  93     3   14       pages.generated
  Pipeline B: 3 runs of the same page, 3 distinct digest(s) — EVERY RUN DIFFERS
      file                                         md5 raw                           md5 class-name normalised         lines  By  members  package
      Pip3Run1.java                                19ea0a03e15f29a27597d1037a04cb89  449f3367e7f860ee0362f8ecc0fdab16  80     11  15       pages
      Pip3Run2.java                                dedd5d2156b53a5b8d5400a90aa4fecc  bfbf6155a16d0422da944a7398d1b043  52     17  18       pageobjects
      Pip3Run3.java                                65662d40ba9231f22536578f36ffcdcb  b25560bf4129f0a20650a015ff523456  50     17  20       pages

  Other Pipeline A artefacts (different pages, not part of this sub-experiment):
      homePageBody.java                            7897b8b39debcee19cce380fe650c7dd  lines=244  By=11

--- Hallucinated API calls (By.dataTest does not exist in org.openqa.selenium.By) ---
  Pipeline B run 1: 11 call(s)
  Pipeline B run 2: 15 call(s)
  Pipeline B run 3: 0 call(s)

--- M2  Locator resolution, evaluated in Chromium against the snapshot ---

  Pipeline A run 1: 5/7 resolve
    FAIL BmItemItem.clickAllItems                   cssSelector  [0/4 instances; matches [0, 0, 0, 0]]
    ok   InventoryItemItem.clickLink                cssSelector  [6/6 instances; matches [2, 2, 2, 2, 2, 2]]
    FAIL InventoryItemItem.clickSauceLabsBackpack   xpath  [1/6 instances; matches [1, 0, 0, 0, 0, 0]]
    ok   InventoryItemItem.clickAddToCart           cssSelector  [6/6 instances; matches [1, 1, 1, 1, 1, 1]]
    ok   byReactBurgerMenuBtn                       cssSelector
    ok   byReactBurgerCrossBtn                      cssSelector
    ok   byShoppingCartLink                         cssSelector

  Pipeline A run 2: 5/7 resolve
    FAIL BmItemItem.clickAllItems                   cssSelector  [0/4 instances; matches [0, 0, 0, 0]]
    ok   InventoryItemItem.clickLink                cssSelector  [6/6 instances; matches [2, 2, 2, 2, 2, 2]]
    FAIL InventoryItemItem.clickSauceLabsBackpack   xpath  [1/6 instances; matches [1, 0, 0, 0, 0, 0]]
    ok   InventoryItemItem.clickAddToCart           cssSelector  [6/6 instances; matches [1, 1, 1, 1, 1, 1]]
    ok   byReactBurgerMenuBtn                       cssSelector
    ok   byReactBurgerCrossBtn                      cssSelector
    ok   byShoppingCartLink                         cssSelector

  Pipeline A run 3: 5/7 resolve
    FAIL BmItemItem.clickAllItems                   cssSelector  [0/4 instances; matches [0, 0, 0, 0]]
    ok   InventoryItemItem.clickLink                cssSelector  [6/6 instances; matches [2, 2, 2, 2, 2, 2]]
    FAIL InventoryItemItem.clickSauceLabsBackpack   xpath  [1/6 instances; matches [1, 0, 0, 0, 0, 0]]
    ok   InventoryItemItem.clickAddToCart           cssSelector  [6/6 instances; matches [1, 1, 1, 1, 1, 1]]
    ok   byReactBurgerMenuBtn                       cssSelector
    ok   byReactBurgerCrossBtn                      cssSelector
    ok   byShoppingCartLink                         cssSelector

  Pipeline B run 1: 2/2 resolve   (+10 locators using a non-existent API — artefact does not compile)
    ok   openMenuBtn                                id
    ok   closeMenuBtn                               id

  Pipeline B run 2: 2/2 resolve   (+15 locators using a non-existent API — artefact does not compile)
    ok   openMenuButton                             id
    ok   closeMenuButton                            id

  Pipeline B run 3: 19/19 resolve
    ok   openMenuButton                             id
    ok   inventorySidebarLink                       cssSelector
    ok   aboutSidebarLink                           cssSelector
    ok   logoutSidebarLink                          cssSelector
    ok   resetSidebarLink                           cssSelector
    ok   closeMenuButton                            id
    ok   shoppingCartLink                           cssSelector
    ok   productSortDropdown                        cssSelector
    ok   addToCartBackpack                          id
    ok   addToCartBikeLight                         id
    ok   addToCartBoltTShirt                        id
    ok   addToCartFleeceJacket                      id
    ok   addToCartOnesie                            id
    ok   addToCartTestTShirt                        id
    ok   twitterLink                                cssSelector
    ok   facebookLink                               cssSelector
    ok   linkedinLink                               cssSelector
    ok   inline in getTitle()                       cssSelector
    ok   inline in getActiveOption()                cssSelector

  Pipeline A after fixes: 7/7 resolve
    ok   BmItemItem.clickLink                       root  [4/4 instances; matches [1, 1, 1, 1]]
    ok   InventoryItemItem.clickLink                cssSelector  [6/6 instances; matches [2, 2, 2, 2, 2, 2]]
    ok   InventoryItemItem.clickLink2               cssSelector  [6/6 instances; matches [2, 2, 2, 2, 2, 2]]
    ok   InventoryItemItem.clickAddToCart           cssSelector  [6/6 instances; matches [1, 1, 1, 1, 1, 1]]
    ok   byReactBurgerMenuBtn                       cssSelector
    ok   byReactBurgerCrossBtn                      cssSelector
    ok   byShoppingCartLink                         cssSelector

--- M3  Locator strategy distribution ---
  Pipeline A run 1: {'cssSelector': 6, 'xpath': 1}
  Pipeline A run 2: {'cssSelector': 6, 'xpath': 1}
  Pipeline A run 3: {'cssSelector': 6, 'xpath': 1}
  Pipeline B run 1: {'id': 2}  + non-existent: {'dataTest': 10}
  Pipeline B run 2: {'id': 2}  + non-existent: {'dataTest': 15}
  Pipeline B run 3: {'id': 8, 'cssSelector': 11}
  Pipeline A after fixes: {'root': 1, 'cssSelector': 6}
```

## M4 — compilability

```
Pipeline A — deterministic generation
  SauceDemOInventoryPagePIPABody.java            COMPILES
  SauceDemoInventoryPagePIPBBody.java            COMPILES
  SauceDemoInventoryPagePIPBBodyRun2Body.java    COMPILES
  SauceDemoInventoryPagePIPBBodyRun3Body.java    COMPILES
  homePageBody.java                              COMPILES
  testinglOGINBody.java                          COMPILES
  ProductsPageBody.java                          COMPILES
  ProductsPageFooter.java                        COMPILES
  ProductsPageHeader.java                        COMPILES
  SauceDemoInventoryPageBody.java                COMPILES

Pipeline B — naive raw-HTML baseline
  Pip3Run1.java  [package pages]                 FAILS
        /tmp/tmp.1jDTt6yDwL/src/pages/Pip3Run1.java:14: error: cannot find symbol
          symbol:   method dataTest(String)
        /tmp/tmp.1jDTt6yDwL/src/pages/Pip3Run1.java:15: error: cannot find symbol
          symbol:   method dataTest(String)
  Pip3Run2.java  [package pageobjects]           FAILS
        /tmp/tmp.1jDTt6yDwL/src/pageobjects/Pip3Run2.java:14: error: cannot find symbol
          symbol:   method dataTest(String)
        /tmp/tmp.1jDTt6yDwL/src/pageobjects/Pip3Run2.java:15: error: cannot find symbol
          symbol:   method dataTest(String)
  Pip3Run3.java  [package pages]                 COMPILES

TOTAL: 11 compile, 2 fail
```
